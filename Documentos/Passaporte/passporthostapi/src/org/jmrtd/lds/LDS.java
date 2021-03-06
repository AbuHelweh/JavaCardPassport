/*
 * JMRTD - A Java API for accessing machine readable travel documents.
 *
 * Copyright (C) 2006 - 2013  The JMRTD team
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * $Id: $
 */

package org.jmrtd.lds;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.jmrtd.PassportService;
import org.jmrtd.io.SplittableInputStream;

/**
 * The logical data structure.
 * 
 * @author The JMRTD team
 * 
 * @version $Revision: $
 * 
 * @since 0.4.8
 */
public class LDS {

	private static final Logger LOGGER = Logger.getLogger("org.jmrtd");

	private Map<Short, LDSFile> files;
	private Map<Short, SplittableInputStream> fetchers;

	public LDS() {
		this.files = new TreeMap<Short, LDSFile>();
		this.fetchers = new TreeMap<Short, SplittableInputStream>();
	}

	public List<Short> getFileList() {
		Set<Short> fileSet = new HashSet<Short>();
		fileSet.addAll(fetchers.keySet());
		fileSet.addAll(files.keySet());
		fileSet.addAll(getDataGroupList());
		if (fileSet.contains(PassportService.EF_DG14)) {
			try {
				DG14File dg14 = getDG14File();
				if (dg14 != null) {
					List<Short> cvcaFIDs = dg14.getCVCAFileIds();
					fileSet.addAll(cvcaFIDs);
				}
			} catch (IOException ioe) {
				LOGGER.severe("Could not read EF.DG14");
			}
		}
		List<Short> fileList = new ArrayList<Short>(fileSet);
		Collections.sort(fileList);
		return fileList;
	}

	public List<Short> getDataGroupList() {
		Set<Short> result = new TreeSet<Short>();
		try {
			COMFile com = getCOMFile();
			int[] comTagList = com.getTagList();
			for (int tag: comTagList) {
				short fid = LDSFileUtil.lookupFIDByTag(tag);
				result.add(fid);
			}
		} catch (IOException ioe) {
			LOGGER.severe("Could not read EF.COM");
		}
		try {
			SODFile sod = getSODFile();
			Set<Integer> dgNumbers = sod.getDataGroupHashes().keySet();
			for (int dgNumber: dgNumbers) {
				short fid = LDSFileUtil.lookupFIDByDataGroupNumber(dgNumber);
				result.add(fid);
			}
		} catch (IOException ioe) {
			LOGGER.severe("Could not read EF.SOd");
		}
		List<Short> resultList = new ArrayList<Short>(result);
		Collections.sort(resultList);
		return resultList;		
	}

	public int getBytesBuffered(short fid) {
		SplittableInputStream fetcher = fetchers.get(fid);
		if (fetcher == null) { return 0; }
		return fetcher.getBytesBuffered();		
	}

	public int getLength(short fid) {
		SplittableInputStream fetcher = fetchers.get(fid);
		if (fetcher == null) { return 0; }
		return fetcher.getLength();
	}

	public int getPosition() {
		int result = 0;
		List<Short> fileList = getFileList();
		for (short fid: fileList) {
			result += getBytesBuffered(fid);
		}
		return result;
	}

	public int getLength() {
		int result = 0;
		List<Short> fileList = getFileList();
		for (short fid: fileList) {
			result += getLength(fid);
		}
		return result;
	}

	public void add(short fid, InputStream inputStream, int length) throws IOException {
		fetchers.put(fid, new SplittableInputStream(inputStream, length));
	}

	public void add(short fid, byte[] bytes) throws IOException {
		add(fid,  new ByteArrayInputStream(bytes), bytes.length);
	}

	public void addAll(Collection<? extends LDSFile> files) {
		for (LDSFile file: files) { add(file); }
	}

	/**
	 * Adds a new file. If the LDS already contained a file
	 * with the same tag, the old copy is replaced. Use this for
	 * constructed files.
	 * 
	 * Note that EF.COM and EF.SOd will not be updated as a result of adding
	 * data groups.
	 * 
	 * @param file the new file to add
	 */
	public void add(LDSFile file) {
		if (file == null) { return; }
		if (file instanceof COMFile) {
			put(PassportService.EF_COM, file);
		} else if (file instanceof SODFile) {
			put(PassportService.EF_SOD, file);
		} else if (file instanceof CVCAFile) {
			CVCAFile cvca = (CVCAFile)file;
			put(cvca.getFID(), cvca);
		} else if (file instanceof DataGroup) {
			DataGroup dataGroup = (DataGroup)file;
			int tag = dataGroup.getTag();
			short fid = LDSFileUtil.lookupFIDByTag(tag);
			put(fid, dataGroup);
		} else {
			throw new IllegalArgumentException("Unsupported LDS file " + file.getClass().getCanonicalName());
		}
	}

	public LDSFile getFile(short fid) throws IOException {
		LDSFile file = files.get(fid);
		if (file != null) {
			return file;
		}

		SplittableInputStream fetcher = fetchers.get(fid);
		if (fetcher == null) {
			throw new IOException("File not available in LDS");
		}
		file = LDSFileUtil.getLDSFile(fid, fetcher.getInputStream(0));
		files.put(fid, file);
		return file;
	}

	public CVCAFile getCVCAFile() throws IOException {
		/* Check DG14 for available CVCA file ids. */
		short cvcaFID = PassportService.EF_CVCA;
		DG14File dg14 = getDG14File();
		if (dg14 == null) { throw new IOException("EF.DF14 not available in LDS"); }
		List<Short> cvcaFIDs = dg14.getCVCAFileIds();
		if (cvcaFIDs != null && cvcaFIDs.size() != 0) {
			if (cvcaFIDs.size() > 1) { LOGGER.warning("More than one CVCA file id present in DG14."); }
			cvcaFID = cvcaFIDs.get(0).shortValue();
		}
		CVCAFile cvca = (CVCAFile)getFile(cvcaFID); // FIXME: should we check for ClassCastException?
		return cvca;
	}

	public InputStream getInputStream(short fid) throws IOException {
		SplittableInputStream fetcher = fetchers.get(fid);
		if (fetcher == null) { throw new IOException("No stream for " + Integer.toHexString(fid)); }
		return fetcher.getInputStream(0);
	}

	public COMFile getCOMFile() throws IOException { return (COMFile)getFile(PassportService.EF_COM); }
	public SODFile getSODFile() throws IOException { return (SODFile)getFile(PassportService.EF_SOD); }
	public DG1File getDG1File() throws IOException { return (DG1File)getFile(PassportService.EF_DG1); }
	public DG2File getDG2File() throws IOException { return (DG2File)getFile(PassportService.EF_DG2); }
	public DG3File getDG3File() throws IOException { return (DG3File)getFile(PassportService.EF_DG3); }
	public DG4File getDG4File() throws IOException { return (DG4File)getFile(PassportService.EF_DG4); }
	public DG5File getDG5File() throws IOException { return (DG5File)getFile(PassportService.EF_DG5); }
	public DG6File getDG6File() throws IOException { return (DG6File)getFile(PassportService.EF_DG6); }
	public DG7File getDG7File() throws IOException { return (DG7File)getFile(PassportService.EF_DG7); }
	public DG11File getDG11File() throws IOException { return (DG11File)getFile(PassportService.EF_DG11); }
	public DG12File getDG12File() throws IOException { return (DG12File)getFile(PassportService.EF_DG12); }
	public DG14File getDG14File() throws IOException { return (DG14File)getFile(PassportService.EF_DG14); }
	public DG15File getDG15File() throws IOException { return (DG15File)getFile(PassportService.EF_DG15); }

	private void put(short fid, LDSFile file) {
		this.files.put(fid, file);
		byte[] bytes = file.getEncoded();
		this.fetchers.put(fid, new SplittableInputStream(new ByteArrayInputStream(bytes), bytes.length));
	}
}
