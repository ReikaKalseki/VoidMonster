varying vec2 texcoord;

uniform sampler2D bgl_RenderedTexture;

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

float dist(vec2 a, vec2 b) {
	float f = float(screenHeight)/float(screenWidth);
	float dx = (a.x-b.x);
	float dy = (a.y-b.y)*f;
	return sqrt(dx*dx+dy*dy);
}

void main() {
    vec4 color = texture2D(bgl_RenderedTexture, texcoord);
    
    float r = color.r;
    float g = color.g;
    float b = color.b;
	
	//float gs = (r+g+b)/3;
	float gs = r*0.2989+g*0.5870+b*0.1140;
	
	/*
	mat4 matrix = projection * modelview;
	vec4 hc =  matrix * vec4(0, 0, 0, 1);
	vec2 monsterXY = clamp((hc / hc.w).xy, 0, 1);
	*//*
	vec4 clipSpacePos = projection * (modelview * vec4(monsterX, monsterY, monsterZ, 1.0));
	vec3 ndcSpacePos = clipSpacePos.xyz / clipSpacePos.w;
	vec2 monsterXY = ((ndcSpacePos.xy + 1.0) / 2.0) * vec2(screenWidth, screenHeight);
	*/
	//vec2 monsterXY = (matrix * vec4(0, 0, 0, 1)).xy;
	vec2 monsterXY = vec2(screenX, screenY);
	
	float dist = dist(monsterXY, texcoord);
	float distfac = max(0, 1-3.5*dist*dist);
	float bf = intensity*distfac;
	
	vec3 net = mix(color.rgb, vec3(gs), bf);
	net = vec3(bf, monsterXY.x, monsterXY.y);
    
    gl_FragColor = vec4(net.x, net.y, net.z, color.a);
}