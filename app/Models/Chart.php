<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;

/**
 * App\Models\Chart
 *
 * @property int $id
 *
 * @mixin \Illuminate\Database\Eloquent\Model
 */
class Chart extends Model
{
    use HasFactory;

    protected $casts = [
        'date' => 'date'
    ];

    public function songs() : BelongsToMany {
        return $this->belongsToMany(Song::class, 'chart_song')->orderby('order');
    }
}
