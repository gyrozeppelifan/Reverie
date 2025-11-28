#version 150

in vec4 Position;
uniform mat4 ProjMat; // Bunu silme, bu gerekli olabilir ama kullanmayacağız

out vec2 texCoord;

void main() {
    // Standart tam ekran çizimi
    vec4 outPos = vec4(Position.xy, 0.0, 1.0);
    gl_Position = vec4(outPos.xy, 0.2, 1.0);

    // Ekran boyutu 1x1 olmadığı için Position 0..1 aralığında gelmez.
    // Ancak PostChain blit işleminde Position genellikle 0..1'e normalize edilmiş gelir.
    // Eğer gelmezse diye garantiye alıyoruz:
    texCoord = Position.xy * 0.5 + 0.5;
}