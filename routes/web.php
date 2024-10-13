<?php

use App\Http\Controllers\ArtistController;
use App\Http\Controllers\ChartController;
use App\Http\Controllers\SongController;
use Illuminate\Support\Facades\Route;

Route::get('/', function () {
    return view('welcome');
});

Route::get('/chart/{id}', [ChartController::class, 'getData']);

Route::get('/song/{id}', [SongController::class, 'getData']);

Route::get('/artist/{id}', [ArtistController::class, 'show']);
