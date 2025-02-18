<?php

namespace App\Enums;

enum RankingType: string
{
    case POINTS = 'points';
    case WEEKS = 'weeks';
    case HIGHEST = 'highest';
}
