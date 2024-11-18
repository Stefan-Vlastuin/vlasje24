<?php

namespace App\Http\Controllers;

use App\Models\Chart;
use Inertia\Inertia;
use Inertia\Response;

class ChartController extends Controller
{
    public function show(string $id): Response
    {
        return Inertia::render('Chart', [
            'chart' => Chart::with('songs.artists')->findOrFail($id)
        ]);
    }
}
