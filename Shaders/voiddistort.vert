varying vec2 texcoord;

uniform int time;
uniform int screenWidth;
uniform int screenHeight;
uniform mat4 modelview;
uniform mat4 projection;
uniform vec3 focus;

uniform float intensity;

float dist(vec2 a, vec2 b) {
	float f = float(screenHeight)/float(screenWidth);
	float dx = (a.x-b.x);
	float dy = (a.y-b.y)*f;
	return sqrt(dx*dx+dy*dy);
}

void main() {
    vec4 vert = gl_Vertex;
	vec3 ndc = vert.xyz / vert.w;
	vec2 vertXY = ((ndc.xy + 1.0) / 2.0);
	
	vec4 clipSpacePos = projection * (modelview * vec4(0, 0, 0, 1.0));
	vec3 ndcSpacePos = clipSpacePos.xyz / clipSpacePos.w;
	vec2 monsterXY = ((ndcSpacePos.xy + 1.0) / 2.0);
	
	float dist = dist(monsterXY, vertXY);
	float distfac = max(0.0, 1.0-3.5*dist*dist);
	float bf = intensity*distfac;

	//vec3 net = mix(vert.xyz, vec3(vert.x, vert.y, vert.z), bf);
	//vert.xyz = net;
	
	vertXY = mix(vertXY, monsterXY, bf);
    
    gl_Position.xyz = gl_ModelViewProjectionMatrix * vert;
    texcoord = vec2(gl_MultiTexCoord0);
}