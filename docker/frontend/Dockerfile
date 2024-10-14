FROM node:20.18-alpine

WORKDIR /var/www

COPY package.json package-lock.json ./

RUN npm install

COPY . .
