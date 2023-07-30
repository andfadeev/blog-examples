import http from 'k6/http';

export const options = {
    stages: [
        { duration: '1h', target: 20 }
    ],
};

export default function () {
    http.get('http://prometheus-monitoring-basics-app:8080/fibonacci');
}
