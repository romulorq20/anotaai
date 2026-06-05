# Resolução: Erro de Alinhamento de 16 KB em Bibliotecas Nativas

## Problema
APK não compatível com dispositivos de 16 KB. A biblioteca nativa `libimage_processing_util_jni.so` não está alinhada em limites de 16 KB.

## Soluções Aplicadas

### 1. Atualização do build.gradle.kts
✅ compileSdk = 36 (mais recente)
✅ targetSdk = 35 (Android 15)
✅ ndkVersion = "26.1.10909125" (NDK com suporte 16 KB)
✅ abiFilters configurados: arm64-v8a, armeabi-v7a, x86, x86_64

### 2. Configuração do CMakeLists.txt
✅ Arquivo criado em app/CMakeLists.txt
✅ Flags de compilação para 16 KB alignment
✅ Suporte para Android 15+

### 3. gradle.properties
✅ android.experimental.enablePackageConfigurationAlignment=true
✅ Suporte para package configuration alignment

## Como Usar

### Opção 1: Build Debug (Recomendado para testes)
```bash
./gradlew clean
./gradlew assembleDebug
```

### Opção 2: Build Release (Para Google Play)
```bash
./gradlew clean
./gradlew assembleRelease
```

### Opção 3: Bundle (Para Google Play - Recomendado)
```bash
./gradlew clean
./gradlew bundleRelease
```

## Validação

Após o build, execute:
```bash
./gradlew validate16KBAlignment
```

## Informações Técnicas

- **Alinhamento de 16 KB**: Google Play requer alinhamento de 16 KB para LOAD segments em bibliotecas nativas (`.so`) para dispositivos Android 15+
- **LOAD segments**: Seções de código executável que precisam estar alinhadas em limites de 16 KB
- **NDK 26.1+**: Compilação automática com alinhamento de 16 KB
- **CMakeLists.txt**: Garante flags corretas de compilação

## Próximas Etapas

1. Execute `./gradlew clean`
2. Execute `./gradlew assembleDebug` ou `./gradlew bundleRelease`
3. Valide com `./gradlew validate16KBAlignment`
4. APK estará pronto para Google Play

## Referências

- Android 16 KB Page Size: https://developer.android.com/16kb-page-size
- NDK Guide: https://developer.android.com/ndk
- Google Play Requirements: https://play.google.com


