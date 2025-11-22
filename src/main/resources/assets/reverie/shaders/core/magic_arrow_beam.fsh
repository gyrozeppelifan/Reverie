#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float GameTime;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    // 1. UV SCROLLING (AKAN ENERJİ)
    // Dokuyu X ekseninde (okun boyu) çok hızlı kaydırıyoruz.
    // Bu, okun kendisi duruyor olsa bile enerjinin aktığı hissini verir.
    vec2 flowUV = texCoord0;
    flowUV.x -= GameTime * 8.0; // Hız

    // 2. RENK (AQUA / CYAN)
    // Dokunun şeklini (Alpha) alıyoruz ama rengi biz veriyoruz.
    vec4 textureColor = texture(Sampler0, texCoord0); // Orijinal şekil için düz UV kullan

    // Enerji dalgası (Kaydırılmış UV ile)
    float energyWave = texture(Sampler0, flowUV).r; // Dokunun desenini enerji olarak kullan

    // Temel Renk: Aqua (R:0, G:1, B:1)
    // Parlaklık: Enerji dalgasıyla birleşiyor
    vec3 beamColor = vec3(0.2, 1.0, 1.0);
    float brightness = 0.8 + (energyWave * 0.5); // 0.8 ile 1.3 arası parlar

    // 3. SONUÇ
    // Alpha: Dokunun kendi şeffaflığı korunur
    fragColor = vec4(beamColor * brightness, textureColor.a) * ColorModulator;
}