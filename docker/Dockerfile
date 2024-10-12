FROM php:8.3-fpm

RUN apt-get update && apt-get install -y \
    git \
    zip unzip libzip-dev \
    && docker-php-ext-install pdo_mysql zip

COPY --from=composer:latest /usr/bin/composer /usr/bin/composer

COPY . .

RUN composer install --no-interaction --prefer-dist --optimize-autoloader
