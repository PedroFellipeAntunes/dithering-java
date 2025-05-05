package Util;

/*
Pedro Fellipe Cruz Antunes
Code that creates a window which receives an n amount of PNG files and changes
their pixels row by row in one of 4 styles:

    Simple chooses a basic type of pixel banding, which simply reduces the values
of the pixels by using rounding math, (brightness * value) / value;

    Floyd-Steinberg style of dithering, which is a type of banding
but creates a dotted pattern to make better gradients by changing the pixels
around the current pixel;

    Jarvis Judice Ninke style of dithering which should be more uniform in
its distribution of dithering;

    Bayer 8x8 style of dithering which is a ordered dithering type, applying
a very specific square pattern to the image which resembles a lot of classic
games;

The output will be n PNG files.

User inputs:
    Drop files;
    Change level with slider;
    Choose which type of image editing;
    Save png file to the same folder as the original file;
*/

import Windows.DropDownWindow;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DropDownWindow dropDownWindow = new DropDownWindow();
        });
    }
}