package br.com.rrrqueiroz.notas.di.module

import br.com.rrrqueiroz.notas.data.datasource.local.NoteLocalDataSource
import br.com.rrrqueiroz.notas.data.datasource.local.RoomNoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    @Singleton
    abstract fun bindNoteLocalDataSource(
        roomNoteDataSource: RoomNoteDataSource
    ): NoteLocalDataSource
}
