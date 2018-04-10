/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

#include "FingerPrintBridgeLib.h"
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <fprint.h>
#include <fp_internal.h>



struct fp_dscv_dev *ddev;
struct fp_dscv_dev **discovered_devs;
struct fp_dev *dev;
struct fp_img_dev *img_dev;
struct fp_print_data *data;

struct fp_dscv_dev *discover_device(struct fp_dscv_dev **discovered_devs) {
    struct fp_dscv_dev *ddev = discovered_devs[0];
    struct fp_driver *drv;
    if (!ddev)
        return NULL;

    drv = fp_dscv_dev_get_driver(ddev);
    printf("Found device claimed by %s driver\n", fp_driver_get_full_name(drv));
    return ddev;
}

int verify(struct fp_dev *dev, struct fp_print_data *data) {
    int r;

    do {
        struct fp_img *img = NULL;

        sleep(1);
        printf("\nScan your finger now.\n");
        fflush(stdout);
        r = fp_verify_finger_img(dev, data, &img);
        if (img) {
            fp_img_save_to_file(img, "verify.pgm");
            printf("Wrote scanned image to verify.pgm\n");
            fflush(stdout);
            fp_img_free(img);
        }
        if (r < 0) {
            printf("verification failed with error %d :(\n", r);
            fflush(stdout);
            return r;
        }
        switch (r) {
            case FP_VERIFY_NO_MATCH:
                printf("NO MATCH!\n");
                return 0;
            case FP_VERIFY_MATCH:
                printf("MATCH!\n");
                return 1;
            case FP_VERIFY_RETRY:
                printf("Scan didn't quite work. Please try again.\n");
                break;
            case FP_VERIFY_RETRY_TOO_SHORT:
                printf("Swipe was too short, please try again.\n");
                break;
            case FP_VERIFY_RETRY_CENTER_FINGER:
                printf("Please center your finger on the sensor and try again.\n");
                break;
            case FP_VERIFY_RETRY_REMOVE_FINGER:
                printf("Please remove finger from the sensor and try again.\n");
                break;
        }
        fflush(stdout);
    } while (1);
}

struct fp_print_data *enroll(struct fp_dev *dev) {
    struct fp_print_data *enrolled_print = NULL;
    int r;
    int i = 0;

    printf("You will need to successfully scan your finger %d times to "
            "complete the process.\n", fp_dev_get_nr_enroll_stages(dev));
    fflush(stdout);

    do {
        i++;
        struct fp_img *img = NULL;

        sleep(1);
        printf("\nScan your finger now.\n");
        fflush(stdout);

        r = fp_enroll_finger_img(dev, &enrolled_print, &img);
        if (img) {
            char * n = malloc(15);
            sprintf(n, "enrolled%d.pgm", i);

            fp_img_save_to_file(img, n);
            printf("Wrote scanned image to ");
            printf("%s\n", n);
            fflush(stdout);
            fp_img_free(img);
        }
        if (r < 0) {
            printf("Enroll failed with error %d\n", r);
            fflush(stdout);
            return NULL;
        }

        switch (r) {
            case FP_ENROLL_COMPLETE:
                printf("Enroll complete!\n");
                break;
            case FP_ENROLL_FAIL:
                printf("Enroll failed, something wen't wrong :(\n");
                return NULL;
            case FP_ENROLL_PASS:
                printf("Enroll stage passed. Yay!\n");
                break;
            case FP_ENROLL_RETRY:
                printf("Didn't quite catch that. Please try again.\n");
                break;
            case FP_ENROLL_RETRY_TOO_SHORT:
                printf("Your swipe was too short, please try again.\n");
                break;
            case FP_ENROLL_RETRY_CENTER_FINGER:
                printf("Didn't catch that, please center your finger on the "
                        "sensor and try again.\n");
                break;
            case FP_ENROLL_RETRY_REMOVE_FINGER:
                printf("Scan failed, please remove your finger and then try "
                        "again.\n");
                break;
        }
        fflush(stdout);
    } while (r != FP_ENROLL_COMPLETE);

    if (!enrolled_print) {
        fprintf(stderr, "Enroll complete but no print?\n");
        fflush(stdout);
        return NULL;
    }

    printf("Enrollment completed!\n\n");
    fflush(stdout);
    return enrolled_print;
}

JNIEXPORT void JNICALL Java_jnitestfingerprint_FPrintController_sayHello
(JNIEnv * env, jobject obj) {

    printf("System On\n");
    fflush(stdout);

}

JNIEXPORT int JNICALL Java_jnitestfingerprint_FPrintController_init
(JNIEnv * env, jobject obj) {
    int r = 1;


    printf("This program will enroll your right index finger, "
            "unconditionally overwriting any right-index print that was enrolled "
            "previously. If you want to continue, press enter, otherwise hit "
            "Ctrl+C\n");
    fflush(stdout);
    getchar();

    r = fp_init();
    if (r < 0) {
        fprintf(stderr, "Failed to initialize libfprint\n");
        fflush(stdout);
        exit(1);
    }
    fp_set_debug(3);

    discovered_devs = fp_discover_devs();
    if (!discovered_devs) {
        fprintf(stderr, "Could not discover devices\n");
        fflush(stdout);
        goto out;
    }

    ddev = discover_device(discovered_devs);
    if (!ddev) {
        fprintf(stderr, "No devices detected.\n");
        fflush(stdout);
        goto out;
    }

    dev = fp_dev_open(ddev);
    fp_dscv_devs_free(discovered_devs);
    if (!dev) {
        fprintf(stderr, "Could not open device.\n");
        fflush(stdout);
        goto out;
    }

    printf("Opened device. It's now time to enroll your finger.\n\n");
    fflush(stdout);
    data = enroll(dev);
    if (!data)
        goto out_close;

    r = fp_print_data_save(data, RIGHT_INDEX);
    if (r < 0)
        fprintf(stderr, "Data save failed, code %d\n", r);
    fflush(stdout);

    fp_print_data_free(data);
out_close:
    fp_dev_close(dev);
out:
    //fp_exit();
    return r;
}

JNIEXPORT jcharArray JNICALL Java_jnitestfingerprint_FPrintController_scanImage(JNIEnv * env, jobject obj, jint * img_height, jint * img_width) {
    int r = 1;

    struct fp_img * img;

    r = fp_init();
    if (r < 0) {
        fprintf(stderr, "Failed to initialize libfprint\n");
        fflush(stdout);
        exit(1);
    }
    fp_set_debug(3);

    discovered_devs = fp_discover_devs();
    if (!discovered_devs) {
        fprintf(stderr, "Could not discover devices\n");
        fflush(stdout);
        exit(1);
    }

    ddev = discover_device(discovered_devs);
    if (!ddev) {
        fprintf(stderr, "No devices detected.\n");
        fflush(stdout);
        exit(1);
    }

    dev = fp_dev_open(ddev);
    fp_dscv_devs_free(discovered_devs);
    if (!dev) {
        fprintf(stderr, "Could not open device.\n");
        fflush(stdout);
        exit(1);
    }

    printf("Opened device. It's now time to scan your finger.\n\n");
    fflush(stdout);

    r = fp_dev_img_capture(dev, 0, &img);
    if (r) {
        fprintf(stderr, "image capture failed, code %d\n", r);
        goto out_close;
    }

    r = fp_img_save_to_file(img, "finger.pgm");
    if (r) {
        fprintf(stderr, "img save failed, code %d\n", r);
        goto out_close;
    }

    fp_img_standardize(img);
    r = fp_img_save_to_file(img, "finger_standardized.pgm");
    //fp_img_free(img);
    /*
    img = fp_img_binarize(img);
    r = fp_img_save_to_file(img, "finger_binarized.pgm");
     */

    printf("%d %d\n", img->width, img->height);
    
    jcharArray image = (*env)->NewCharArray(env, (img->length / sizeof (char)));
    /*
    for(int i = 0; i < img->length; i++){
        printf("%d\n",img->data[i]);
        fflush(stdout);
    }
     */
    jchar *body = (*env)->GetCharArrayElements(env, image, 0);

    for (int i = 0; i < img->length; i++) {
        body[i] = img->data[i];
        //printf("%d\n",body[i]);
        //fflush(stdout);
    }

    (*env)->ReleaseCharArrayElements(env, image, body, 0);

    fp_img_free(img);

    if (r) {
        fprintf(stderr, "standardized/binarize img save failed, code %d\n", r);
        goto out_close;
    }

    r = 0;
out_close:
    fp_dev_close(dev);
out:
    fp_exit();
    return image;
}

JNIEXPORT jboolean JNICALL Java_jnitestfingerprint_FPrintController_verifyImage(JNIEnv * env, jobject obj, jcharArray imageBytes, jint img_height, jint img_width) {
    int r = 1;


    r = fp_init();
    if (r < 0) {
        fprintf(stderr, "Failed to initialize libfprint\n");
        fflush(stdout);
        exit(1);
    }
    fp_set_debug(3);

    discovered_devs = fp_discover_devs();
    if (!discovered_devs) {
        fprintf(stderr, "Could not discover devices\n");
        fflush(stdout);
        return -1;
    }

    ddev = discover_device(discovered_devs);
    if (!ddev) {
        fprintf(stderr, "No devices detected.\n");
        fflush(stdout);
        return -2;
    }

    dev = fp_dev_open(ddev);
    fp_dscv_devs_free(discovered_devs);
    if (!dev) {
        fprintf(stderr, "Could not open device.\n");
        fflush(stdout);
        return -3;
    }

    img_dev = dev->priv;
    if (!img_dev) {
        fprintf(stderr, "DEBUG: img_dev null\n");
        fflush(stdout);
        return -4;
    }

    struct fp_img * img = fpi_img_new((*env)->GetArrayLength(env, imageBytes) * sizeof (char));
    struct fp_img_driver *driver = fpi_driver_to_img_driver(dev->drv);
    
    img->height = driver->img_height > 0 ? driver->img_height : img_height; //no meu caso varia , então vem -1
    img->width = driver->img_width > 0 ? driver->img_width : img_width;
    printf("%d %d", img->width,  img->height);


    jchar *body = (*env)->GetCharArrayElements(env, imageBytes, 0);

    for (int i = 0; i < img->length; i++) {
        img->data[i] = body[i];
        //printf("%d\n",body[i]);
        //fflush(stdout);
    }

    (*env)->ReleaseCharArrayElements(env, imageBytes, body, 0);



    struct fp_print_data * data;

    fpi_img_to_print_data(img_dev, img, &data);

    

    return verify(dev, data);
}
