# Dither

This project is a Java Maven application built with NetBeans IDE and Swing that applies a variety of **dithering** and **color quantization** methods to PNG/JPG/JPEG images, with options for color levels, processing scale, grayscale conversion, and dynamic range.

<p align="center">
  <img src="examples/ds3_gael_original_bayer.png" width="350" alt="Example Bayer 8×8">
  <img src="examples/ds3_gael_original_Atkinson.png" width="350" alt="Example Atkinson">
</p>
<p align="center">
  <img src="examples/ds3_gael_simple_norange.png" width="350" alt="Example Simple no Range">
  <img src="examples/ds3_gael_simple_withrange.png" width="350" alt="Example Simple with Range">
</p>

## Features

- **Multiple Dithering Algorithms**
  - Ordered Dithering: Bayer 8×8 (Which is calculted during runtime)
  - Diffusion Dithering: Floyd–Steinberg, Jarvis–Judice–Ninke (JJN), Stucki, Atkinson, Burkes, Sierra
- **Simple Quantization**: reduces color levels without dithering
- **Adjustable Color Levels**: from 2 up to 256
- **Adjustable Spread**: defining how much dither will be applied
- **Processing Scale**: down and up scaling factor (1 to 5)
- **Grayscale Mode**: convert image to gray before processing
- **Dynamic Range Toggle**: enable or disable dynamic range in quantization
- **Drag & Drop UI**: process multiple images at once by dragging PNG/JPG/JPEG files
- **Live Preview** before saving
- **Automatic Naming** of output based on chosen parameters

## **How to use**

**1.** Execute the `.jar` file located in the `/target` folder: `ColorBanding.jar`.

**2.** Change the settings to desired values.

**3.** Drag and drop the image(s) into the interface.

**4.** Preview the changed image before saving.

**5.** After it's done, the image(s) will be saved to the same folder as the original with the name: `originalname_Quantize[type,color_levels,scale,spread].png`
