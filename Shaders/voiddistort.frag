#import math
#import geometry
#import color

uniform float distance;

float scaleFactor(float dist) {
    return 1.0+intensity*min(1.0, distance/5.0)*max(0.0, min(1.0, max(0.0, 18.0/distance))*min(4.0, 0.009/dist));
}

void main() {
	vec2 monsterXY = getScreenPos(0.0, 0.5, 0.0);
	
    vec4 color = texture2D(bgl_RenderedTexture, texcoord);
	
	float distv = distsq(monsterXY, texcoord);
	float distfac_color = max(0.0, 1.0-3.5*distv);
	float cf = intensity*distfac_color;
	
	vec2 diff = texcoord-monsterXY;
	diff *= scaleFactor(max(0.00001, distv));
	vec2 texUV = monsterXY+diff;
	
    color = texture2D(bgl_RenderedTexture, texUV);
    
    float gs = getVisualBrightness(color.rgb);
	
	vec3 net = mix(color.rgb, vec3(gs), cf);
    
    gl_FragColor = vec4(net.x, net.y, net.z, color.a);
}