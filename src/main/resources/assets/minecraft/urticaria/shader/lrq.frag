#version 120

uniform vec2 u_size;
uniform float u_radius;
uniform vec4 u_color;

void main(void)
{
    vec2 rectHalf = u_size * .5;
    gl_FragColor = vec4(u_color.rgb, u_color.a * (1.0 - smoothstep(0.0, 1.0, length(max(abs(rectHalf - (gl_TexCoord[0].st * u_size)) - rectHalf - u_radius - 1, 0.0) - u_radius))));
}