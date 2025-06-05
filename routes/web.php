<?php

use App\Http\Controllers\ArtistController;
use App\Http\Controllers\ChartController;
use App\Http\Controllers\SongController;
use App\Http\Controllers\RankingController;
use App\Http\Controllers\SearchController;
use Illuminate\Support\Facades\Route;

Route::get('/', [ChartController::class, 'redirectToLatest']);
Route::get('/chart/{id}', [ChartController::class, 'show']);

Route::get('/song/{id}', [SongController::class, 'show']);

Route::get('/artist/{id}', [ArtistController::class, 'show']);

Route::get('/ranking/{year}', [RankingController::class, 'show']);

Route::get('/search', [SearchController::class, 'search']);
