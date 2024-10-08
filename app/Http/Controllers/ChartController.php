<?php

namespace App\Http\Controllers;

use App\Models\Chart;
use Illuminate\View\View;

class ChartController extends Controller
{
    public function show(string $id): View
    {
        return view('chart.show', [
            'chart' => Chart::findOrFail($id)
        ]);
    }
}
