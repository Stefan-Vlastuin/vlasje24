<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('song_artist', function (Blueprint $table) {
            $table->id();
            $table->foreignId('song_id')->constrained()->onDelete('cascade');
            $table->foreignId('artist_id')->constrained()->onDelete('cascade');
            $table->unsignedInteger('order')->default(1);
            $table->timestamps();

            $table->unique(['artist_id', 'song_id']);
            $table->unique(['song_id', 'order']);
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('song_artist');
    }
};
