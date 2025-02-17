<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Casts\Attribute;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;
use Illuminate\Support\Collection;

/**
 * @property int $id
 * @property Collection $charts
 * @property int $points
 * @property int $nrOfWeeks
 * @property int $highestPosition
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

    protected function points(): Attribute
    {
        return Attribute::make(
            get: fn () => $this->getPoints(),
        );
    }

    protected function nrOfWeeks(): Attribute
    {
        return Attribute::make(
            get: fn () => $this->getNrOfWeeks(),
        );
    }

    protected function highestPosition(): Attribute
    {
        return Attribute::make(
            get: fn () => $this->getHighestPosition(),
        );
    }

    public function getPoints() : int {
        return $this->charts->map(function (Chart $chart) {
            return 25 - $chart->pivot->order;
        })->sum();
    }

    public function getNrOfWeeks() : int {
        return $this->charts->count();
    }

    public function getHighestPosition() : int {
        return $this->charts->map(function (Chart $chart) {
            return $chart->pivot->order;
        })->min();
    }
}
