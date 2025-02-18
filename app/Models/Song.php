<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;
use Illuminate\Support\Collection;

/**
 * @property int $id
 * @property Collection $charts
 */
class Song extends Model
{
    use HasFactory;

    public function artists() : BelongsToMany {
        return $this->belongsToMany(Artist::class, 'song_artist')->orderBy('order')->withTimestamps();
    }

    public function charts() : BelongsToMany {
        return $this->belongsToMany(Chart::class, 'chart_song')->withPivot('order')->withTimestamps();
    }

    public function getPoints(?int $year) : int {
        return $this->filterCharts($year)->map(function (Chart $chart) {
            return 25 - $chart->pivot->order;
        })->sum();
    }

    public function getNrOfWeeks(?int $year) : int {
        return $this->filterCharts($year)->count();
    }

    public function getHighestPosition(?int $year) : int {
        return $this->filterCharts($year)->map(function (Chart $chart) {
            return $chart->pivot->order;
        })->min();
    }

    private function filterCharts(?int $year) : Collection {
        return $this->charts->filter(function (Chart $chart) use ($year) {
            return $year === null || $chart->date->year === $year;
        });
    }
}
