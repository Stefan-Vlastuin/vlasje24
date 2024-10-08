@extends('layouts.app')

@section('content')
    <div class="container">
        <h1>Chart for Week of {{ $chart->date->format('F j, Y') }}</h1>

        <table class="table">
            <thead>
            <tr>
                <th>Position</th>
                <th>Song Name</th>
                <th>Artist</th>
            </tr>
            </thead>
            <tbody>
            @foreach($chart->songs as $song)
                <tr>
                    <td>{{ $song->pivot->order }}</td>
                    <td>{{ $song->name }}</td>
                </tr>
            @endforeach
            </tbody>
        </table>
    </div>
@endsection
