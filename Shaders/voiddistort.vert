varying vec2 texcoord;

uniform int time;
uniform int screenWidth;
uniform int screenHeight;
uniform mat4 modelview;
uniform mat4 projection;

uniform float intensity;
//uniform float monsterX;
//uniform float monsterY;
//uniform float monsterZ;

uniform float screenX;
uniform float screenY;

void main() {
    vec4 vert = gl_Vertex;
	
	mat4 matrix = projection * modelview;
	
	vec2 monsterXY = vec2(screenX, screenY);
	
    vert.x = vert.x*1.0;
    vert.y = vert.y*(1.0+intensity*0.00001+monsterXY.x*0.000001);
    vert.z = vert.z*1.0;
    
    gl_Position = gl_ModelViewProjectionMatrix * vert;
    texcoord = vec2(gl_MultiTexCoord0);
}