# ImageEditingApp: A computer vision application focused on spatial and projective image transformations.

## Cropping Functionalities
The project is divided into three distinct functionalities involved in cropping an image using the tool. They are as follows:

### Resizing the image and crop rectangle:
Resizing an image and reshaping the cropping rectangle is one of the fundamental features required for any image editing tool. The resizing image feature can be implemented using a magnification, which is done using a scaling variable (positive scalar).


The scaling must be with respect to the focal point on the screen, where the user performs the scaling. This can be easily implemented using the normalized scaling method, in which first we translate the focal point to the origin, then we scale it, and then re-translate the focal point. Mathematically, it is given by,
$$ P_{scaled} = s_c(P - p_f) + p_f $$

The reshaping of the crop rectangle is implemented by moving the edges of the rectangle based on the user inputs.

There are two constraints involved in this operation:



1.   The cropping rectangle must not exceed the dimensions of the image in the background.
2.   The edges of the image must not go within the edges of the cropping rectangle while scaling the image.

The preliminary approach to solve this is to impose edge-based constraints. However, this would be improved as when rotation is involved.

### Moving the image
This functionality is referred to as panning, and it can be implemented using a shift operation, as given below.

$$P_{pan} = P + \Delta P$$

The same edge constraint can be used in this operation to limit the image's edges from crossing over the cropped rectangle's edges. In case of rotation, this logic needs further improvement.


### Rotation
The most crucial and challenging functionality among the required operations is rotation because the edge-based constraints will not work when the edges are not parallel.

First, we implement the rotation constraint by forming a rotation matrix, and multiplying all the points with it. Similar to the scaling operation, this operation also needs normalization about the focal point (center of rotation).
$$
R = \begin{bmatrix}
\text{cos}(\theta) & -\text{sin}(\theta) \\
\text{sin}(\theta) & \text{cos}(\theta)
\end{bmatrix} \\
P_{rotated} = R (P - p_f) + p_f
$$

The constraint required with the rotation is to scale the image appropriately during the rotation such that there is no blank space inside the cropped rectangle. Here, we ensure that all the points of the cropped rectangle remain within the image.

Now, we must ensure that the four corners of the cropped rectangle must remain within the edges of the image.

1. For this, we must represent the corner points of the rectangle in the reference frame of the image. So, we apply the inverse rotation on the corner points of the cropped rectangle.
$$
\begin{bmatrix}
x_r \\ y_r
 \end{bmatrix} = R^{-1} (p_{corner} - p_o) + p_o
$$
Thus, we obtain the rotated corner points $(x_1, y_1)$, $(x_2, y_2)$, $(x_3, y_3)$, and $(x_4, y_4)$ of the cropped rectangle. Note that in the image's reference frame, its edges are aligned with its X and Y axes. So, it is easy to check whether the rotated corner points are inside the image.

2. Now, we compare all the points with the edges of the image square such that $X_min \leq x1, x2, x3, x4 \leq X_max$ and $Y_min \leq y1, y2, y3, y4 \leq Y_max$, where $(X_min, X_max, Y_min, Y_max)$ are the edges of the image. In other words, the scaled boundaries of the image must be maintained to be larger than the corners of the cropped rectangle. The constraints can be written as,
$$
S_{min}(X_{min} - x_0) + x_0 \leq x1, x2, x3, x4 \leq S_{min}(X_{max} - x_0) + x_0 \\
S_{min}(Y_{min} - y_0) + y_0 \leq y1, y2, y3, y4 \leq S_{min}(Y_{max} - y_0) + y_0
$$

3. While there exists different methods to implement the scale, here we use an iterative method, in which the scale is increased in small increments iteratively in a loop until all the constraints are satisfied.


### Remark on the previous operations
The same logic used for rescaling the image to fit the cropped rectangle within the rotated image can be used in the zooming and panning operations with rotation. Here, instead of checking the edge constraints, we transform the basis of the corner points and check whether they are bounded within the edges of the image.

## Tech Stack

Platform and language: Android, Kotlin
