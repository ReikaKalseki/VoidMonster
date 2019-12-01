varying vec2 texcoord;

uniform sampler2D bgl_RenderedTexture;

uniform int time;
uniform int screenWidth;
uniform int screenHeight;
uniform mat4 modelview;
uniform mat4 projection;
uniform vec3 focus;

uniform float intensity;

float distsq(vec2 a, vec2 b) {
	float f = float(screenHeight)/float(screenWidth);
	float dx = (a.x-b.x);
	float dy = (a.y-b.y)*f;
	return dx*dx+dy*dy;
}

float scaleFactor(vec2 a, vec2 b){
    return 1.0+intensity/pow(distsq(a, b)+1.0, 32);
}

void main() {
	vec4 clipSpacePos = projection * (modelview * vec4(0, 0, 0, 1.0));
	vec3 ndcSpacePos = clipSpacePos.xyz / clipSpacePos.w;
	vec2 monsterXY = ((ndcSpacePos.xy + 1.0) / 2.0);
	
	float distsq = distsq(monsterXY, texcoord);
	float distfac_color = max(0.0, 1.0-3.5*distsq);
	float cf = intensity*distfac_color;
	
	vec2 diff = normalize(texcoord-monsterXY);
	diff *= scaleFactor(texcoord, monsterXY);
	texcoord = monsterXY+diff;
	
    vec4 color = texture2D(bgl_RenderedTexture, texcoord);
    
    float r = color.r;
    float g = color.g;
    float b = color.b;
	
	//float gs = (r+g+b)/3;
	float gs = r*0.2989+g*0.5870+b*0.1140;
	
	vec3 net = mix(color.rgb, vec3(gs), cf);
    
    gl_FragColor = vec4(net.x, net.y, net.z, color.a);
}