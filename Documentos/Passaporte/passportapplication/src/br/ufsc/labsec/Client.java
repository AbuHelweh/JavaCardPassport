package br.ufsc.labsec;

import java.net.URL;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class Client {
	//private static final String urlServidor = "http://localhost:8185";
	private static final String urlServidor = "http://127.0.0.1/projects/server.php";
	private XmlRpcClient xmlrpc;

	public Client() {
		try {
			XmlRpcClientConfigImpl configuracaoCliente = new XmlRpcClientConfigImpl();
			configuracaoCliente.setServerURL(new URL(urlServidor));

			xmlrpc = new XmlRpcClient();
			xmlrpc.setConfig(configuracaoCliente);

		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public Object executar(String comando, Object[] parametros) {
		try {
			Object resposta = xmlrpc.execute(comando, parametros);
			return resposta;
		} catch (XmlRpcException e) {
			e.printStackTrace();
			return null;
		}
	}
}
