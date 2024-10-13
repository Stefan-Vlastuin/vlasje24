<?php

namespace Database\Seeders;

use App\Models\Artist;
use App\Models\Chart;
use App\Models\Song;
use App\Models\User;
use Illuminate\Database\Seeder;

class DatabaseSeeder extends Seeder
{
    /**
     * Seed the application's database.
     */
    public function run(): void
    {
        // User::factory(10)->create();

        User::factory()->create([
            'name' => 'Test User',
            'email' => 'test@example.com',
        ]);

        $artists = Artist::factory()->createMany([
            ['id' => 1, 'name' => 'Rosa Linn'],
            ['id' => 2, 'name' => 'Kygo'],
            ['id' => 3, 'name' => 'Sam Feldt'],
            ['id' => 4, 'name' => 'Imagine Dragons'],
        ]);

        /** @var Song $song1 */
        $song1 = Song::factory()->create(['title' => 'Snap']);
        $song1->artists()->attach($artists[0]->id, ['order' => 1]);

        /** @var Song $song2 */
        $song2 = Song::factory()->create(['title' => 'Heart Like Mine']);
        $song2->artists()->attach([
            $artists[0]->id => ['order' => 1],
            $artists[2]->id => ['order' => 2],
        ]);

        /** @var Song $song3 */
        $song3 = Song::factory()->create(['title' => 'Stars Will Align']);
        $song3->artists()->attach([
            $artists[1]->id => ['order' => 1],
            $artists[3]->id => ['order' => 2],
        ]);

        /** @var Song $song4 */
        $song4 = Song::factory()->create(['title' => 'Never Be Mine']);
        $song4->artists()->attach($artists[0]->id, ['order' => 1]);

        /** @var Chart $chart1 */
        $chart1 = Chart::factory()->create(['date' => '2024-01-01']);
        $chart1->songs()->attach([
            $song1->id => ['order' => 1],
            $song2->id => ['order' => 2],
            $song3->id => ['order' => 3],
        ]);

        /** @var Chart $chart2 */
        $chart2 = Chart::factory()->create(['date' => '2024-01-08']);
        $chart2->songs()->attach([
            $song3->id => ['order' => 1],
            $song1->id => ['order' => 2],
            $song4->id => ['order' => 3],
        ]);

        /** @var Chart $chart3 */
        $chart3 = Chart::factory()->create(['date' => '2024-01-15']);
        $chart3->songs()->attach([
            $song1->id => ['order' => 1],
            $song3->id => ['order' => 2],
            $song4->id => ['order' => 3],
        ]);

    }
}
