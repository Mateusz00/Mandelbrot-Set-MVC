# About
Java application, using MVC design pattern, which allows users to generate beautiful fractals. It lets users to conveniently zoom in/out and move center by mouse or keyboard, generate images and videos of mandelbrot set.

# Theory
[Mandelbrot set](http://en.wikipedia.org/wiki/Mandelbrot_set) is a set of points in the complex plane for which the orbit of z<sub>n</sub> doesn't tend to infinity. The iteration formula used in the Mandelbrot set is:

Z<sub>0</sub> = 0</br>
Z<sub>n+1</sub> = Z<sub>n</sub><sup>2</sup> + C</br>
Where C determines the location of the iteration series in the complex plane.


# Calculating the Mandelbrot Set
* If the magnitude of Z ever becomes larger than declared threshold value, we will assume that it will diverge into infinity.
* If the number of iterations exceeds declared maximum iterations value, we will assume that Z doesn't tend to infinity.
* We will speed up calculations by using Divide and Conquer approach, calculating each part of set in different thread (Can be easily implemented with Fork/Join in java)

# Coloring the plot
In order to colorize the plot we take number of iterations performed and map that against a color spectrum.

# Zooming
Zooming is achieved by decreasing/increasing mandelbrot set range

# Examples
For more examples go [here](http://github.com/Mateusz00/Mandelbrot-Set-MVC/tree/master/Examples)
<img src="https://github.com/Mateusz00/Mandelbrot-Set-MVC/blob/master/Examples/example01.png" alt="example01"/>
<img src="https://github.com/Mateusz00/Mandelbrot-Set-MVC/blob/master/Examples/example03.png" alt="example03"/>
<img src="https://github.com/Mateusz00/Mandelbrot-Set-MVC/blob/master/Examples/example05.png" alt="example05"/>
