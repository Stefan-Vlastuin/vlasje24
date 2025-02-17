<?php

use App\Http\Controllers\ArtistController;
use App\Http\Controllers\ChartController;
use App\Http\Controllers\SongController;
use App\Http\Controllers\JaaroverzichtController;
use Illuminate\Support\Facades\Route;

Route::get('/', function () {
    return view('welcome');
});

Route::get('/chart/{id}', [ChartController::class, 'show']);

Route::get('/song/{id}', [SongController::class, 'show']);

Route::get('/artist/{id}', [ArtistController::class, 'show']);

Route::get('/jaaroverzicht', [JaaroverzichtController::class, 'index']);
