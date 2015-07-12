/*
 * Copyright (c) 2015 Franco Sebregondi.
 */

package com.example.android.spotifystreamer.util;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Utility class for work with images
 */
public class ImageUtils {

    /**
     * Returns the image with the closest dimensions to the targetSize from a list of images.
     * <p/>
     * A heuristic approach is used to determine the image which the closest dimensions.
     * One assumption is that the width / height ratio of the images are very small (image
     * dimension is close to a square).
     *
     * @param images     a list of Images
     * @param targetSize target image size (square).
     *                   return the image with the closes size
     */
    public static Image findImageWithClosestSize(List<Image> images, int targetSize) {


        Image closestImage = null;
        int closestDeltaSize = Integer.MAX_VALUE;

        for (Image image : images) {

            // Get a number we can use to compare to targetSize. It is kind a arbitrary
            // to use the minimum of the image rect.
            int size = Math.min(image.height, image.width);
            int deltaToTargetSize = Math.abs(targetSize - size);

            if (deltaToTargetSize < closestDeltaSize) {
                closestImage = image;
                closestDeltaSize = deltaToTargetSize;
            }
        }

        return closestImage;
    }
}
