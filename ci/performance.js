'use strict';
const { URL } = require('url');


module.exports = {

    name() {
        return 'gitlab-exporter';
    },

    open(context) {
        this.storageManager = context.storageManager;
        this.results = [];
    },

    processMessage(message) {

        if ( message.type != 'coach.pageSummary') {
            return;
        }

        const urlPath = new URL(message.url);

        this.results.push({
            'subject' : urlPath.pathname,
            'metrics' : [{
                'name' : 'Browser Performance',
                'value' : message.data.advice.performance.score
            }]
        });

    },

    close(options, errors) {
        return this.storageManager.writeData(JSON.stringify(this.results), 'performance.json');
    }
};
