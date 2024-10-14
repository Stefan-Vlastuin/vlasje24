# Setup with Docker

1. Clone the repository and enter it.
2. Create a ``.env`` file.
    ```shell
    cp .env.example .env
    ```
3. Start the containers.
    ```shell
    docker compose up -d --build
    ```
4. Generate a key for Artisan.
    ```shell
    docker compose exec app php artisan key:generate
    ```
5. Start the Vite development server.
    ```shell
    docker compose exec npm npm run dev
    ```
6. Visit the website at <http://localhost:8000/>.
