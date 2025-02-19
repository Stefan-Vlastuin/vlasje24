<?php

namespace App\Http\Controllers;

use App\Models\Song;
use Illuminate\Http\Request;
use Illuminate\Http\JsonResponse;

class SearchController extends Controller
{
    public function search(Request $request): JsonResponse
    {
        $query = $request->input('q');
        $results = Song::where('title', 'LIKE', "%{$query}%")->get();

        return response()->json($results);
    }
}
