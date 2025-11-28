#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vertexColor;
out vec2 texCoord0;

// OUTLINE KALINLIĞI
// Burayı artırırsan çizgi kalınlaşır. 0.06 - 0.10 arası iyidir.
const float INFLATE = 0.08;

void main() {
    // Normal vektörü yönünde şişirme yapıyoruz
    vec3 expandedPos = Position + (Normal * INFLATE);

    gl_Position = ProjMat * ModelViewMat * vec4(expandedPos, 1.0);
    vertexColor = Color;
    texCoord0 = UV0;
}