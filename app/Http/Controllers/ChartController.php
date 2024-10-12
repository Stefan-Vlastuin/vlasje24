<?php

namespace App\Http\Controllers;

use App\Models\Chart;
use Illuminate\Http\JsonResponse;

class ChartController extends Controller
{
    public function getData(string $id): JsonResponse
    {
        $chart = Chart::with('songs.artists')->findOrFail($id);
        return response()->json($chart);
    }
}
