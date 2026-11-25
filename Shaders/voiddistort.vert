#import math

uniform float longIntensity;

void main() {
    vec4 vert = gl_Vertex;
	gl_Position = gl_ModelViewProjectionMatrix * vert;
    vec2 offset = vec2(rand(time), rand(time + 17.31)) * longIntensity*0.001;
    gl_Position.xy += offset * gl_Position.w;
    texcoord = vec2(gl_MultiTexCoord0);
}