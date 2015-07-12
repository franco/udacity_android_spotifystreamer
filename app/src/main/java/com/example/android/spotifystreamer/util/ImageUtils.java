/*
 * Copyright (c) 2015 Franco Sebregondi.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
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
