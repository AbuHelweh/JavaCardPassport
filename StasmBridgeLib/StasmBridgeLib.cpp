/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

#include "StasmBridgeLib.h"
#include <stdio.h>
#include <stdlib.h>
#include "opencv2/highgui.hpp"
#include "opencv2/imgcodecs.hpp"
#include "opencv2/core.hpp"
#include "opencv2/objdetect.hpp"
#include "stasm.h"
#include "stasm_lib.h"
#include "stasm_landmarks.h"
#include "stasm_lib_ext.h"

JNIEXPORT jfloatArray JNICALL Java_stasmlib_StasmController_getImageFeaturePoints
(JNIEnv *env, jobject obj, jstring filename) {

    char* path = (char*) env->GetStringUTFChars(filename, 0);

    cv::Mat_<unsigned char> img(cv::imread(path, CV_LOAD_IMAGE_GRAYSCALE));

    if (!img.data) {
        printf("Cannot load %s\n", path);
        exit(1);
    }

    int foundface;
    float landmarks[2 * stasm_NLANDMARKS]; // x,y coords (note the 2)
    if (!stasm_search_single(&foundface, landmarks,
            (const char*) img.data, img.cols, img.rows, path, "../data")) {
        printf("Error in stasm_search_single: %s\n", stasm_lasterr());
        exit(1);
    }

    if (!foundface)
        printf("No face found in %s\n", path);
    else {
        // draw the landmarks on the image as white dots (image is monochrome)
        stasm_force_points_into_image(landmarks, img.cols, img.rows);
        for (int i = 0; i < stasm_NLANDMARKS; i++)
            cv::circle(img,cv::Point(cvRound(landmarks[i * 2]), cvRound(landmarks[i * 2 + 1])),2,cv::Scalar(255,255,255),-1);
    }
    
    cv::imwrite("minimal.jpg", img);
    cv::imshow("stasm minimal", img);
    cv::waitKey();

    jfloatArray ret = env->NewFloatArray(stasm_NLANDMARKS * 2);

    jfloat* body = env->GetFloatArrayElements(ret, 0);

    for (int i = 0; i < stasm_NLANDMARKS * 2; i++) {
        body[i] = landmarks[i];
    }

    return ret;

}