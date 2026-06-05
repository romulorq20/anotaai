package br.com.rrrqueiroz.notas.di.module

import android.content.Context
import br.com.rrrqueiroz.notas.utils.FileUtils

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class FileUtilsModule {
    @Singleton
    @Provides
    fun provideFileUtils(@ApplicationContext context: Context): FileUtils {
        return FileUtils(context)
    }
}

