#version 150
in vec2 TexCoord0;
out vec4 FragColor;
uniform sampler2D uDiffuseSampler;

void main() {
    FragColor = texture(uDiffuseSampler, TexCoord0);
}
