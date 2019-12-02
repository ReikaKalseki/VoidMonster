float scaleFactor(vec2 a, vec2 b){
    return 1.0/pow(distsq(a, b)*8.0+1.0+0.5, 2.0);
}

void main() {
	vec2 monsterXY = getScreenPos(0.0, 0.0, 0.0);
	
	float distv = distsq(monsterXY, texcoord);
	float distfac_color = max(0.0, 1.0-3.5*distv);
	float cf = intensity*distfac_color;
	
	vec2 diff = normalize(texcoord-monsterXY);
	diff *= scaleFactor(texcoord, monsterXY);
	texcoord = monsterXY+diff;
	
    vec4 color = texture2D(bgl_RenderedTexture, texcoord);
    
    float gs = getVisualBrightness(color.rgb);
	
	vec3 net = mix(color.rgb, vec3(gs), cf);
    
    gl_FragColor = vec4(net.x, net.y, net.z, color.a);
}