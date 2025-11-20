#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float GameTime;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
  float time = GameTime * 1000.0;

  // 1. Yavaş, Derin Nabız
  float pulse = (sin(time) + 1.0) * 0.5;

  // 2. Blur (Yayılma)
  float spread = 0.03;
  vec4 blurSum = vec4(0.0);

  // 3x3 Örnekleme
  blurSum += texture(Sampler0, texCoord0 + vec2(-spread, -spread));
  blurSum += texture(Sampler0, texCoord0 + vec2( 0.0,    -spread));
  blurSum += texture(Sampler0, texCoord0 + vec2( spread, -spread));
  blurSum += texture(Sampler0, texCoord0 + vec2(-spread,  0.0));
  blurSum += texture(Sampler0, texCoord0 + vec2( 0.0,     0.0));
  blurSum += texture(Sampler0, texCoord0 + vec2( spread,  0.0));
  blurSum += texture(Sampler0, texCoord0 + vec2(-spread,  spread));
  blurSum += texture(Sampler0, texCoord0 + vec2( 0.0,     spread));
  blurSum += texture(Sampler0, texCoord0 + vec2( spread,  spread));

  vec4 blurredTex = blurSum / 9.0;

  // 3. Halka Efekti (Kenarları Sil)
  // Oyuncu skini daha karmaşık olduğu için distance yerine alpha'yı kullanıyoruz
  if (blurredTex.a < 0.1) discard;

  // 4. RENK: AQUA MAVİ (Cyan)
  vec3 glowColor = vec3(0.0, 0.9, 1.0);

  // Parlaklık
  float brightness = 0.5 + (pulse * 0.3);

  // Alpha (Şeffaflık)
  // blurredTex.a'yı kullanarak oyuncunun şeklini alıyoruz
  float finalAlpha = blurredTex.a * brightness * 0.6;

  fragColor = vec4(glowColor, finalAlpha) * ColorModulator;
}