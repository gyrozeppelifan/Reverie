#version 150

uniform sampler2D Sampler0;
uniform float GameTime;
uniform vec4 FlashColor;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    // INVERTED HULL HİLESİ:
    // Eğer bu yüzey kameraya bakıyorsa (Ön Yüz), çizme.
    // Böylece sadece modelin arkasındaki dış kabuğu görürüz.
    if (gl_FrontFacing) {
        discard;
    }

    vec4 textureColor = texture(Sampler0, texCoord0);

    // Texture'da boşluk olan yerleri boyama
    if (textureColor.a < 0.1) {
        discard;
    }

    // Titreme Animasyonu
    float flash = abs(sin(GameTime * 4000.0));
    float alpha = (flash * 0.5 + 0.5) * FlashColor.a;

    // Neon Rengi Bas
    fragColor = vec4(FlashColor.rgb, alpha);
}