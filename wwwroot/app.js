/// <reference path="node_modules/restify/lib/index.js" />
/// <reference path="node_modules/async/lib/async.js" />

var restify = require('restify');
var async = require('async');

var server = restify.createServer({
  name: 'vasttrafik',
  version: '1.0.0'
});
server.use(restify.acceptParser(server.acceptable));
server.use(restify.queryParser());
server.use(restify.bodyParser());

var fixArrays = function(obj) {
  if (!obj.TripList.Trip.length)
    obj.TripList.Trip = [obj.TripList.Trip];
  for (var i = 0; i < obj.TripList.Trip.length; i++) {
    var trip = obj.TripList.Trip[i];
    if (!trip.Leg.length)
      trip.Leg = [trip.Leg];
  }
};

var searchTrip = function (date, time, item, callback) {
  var client = restify.createJsonClient({
    url: 'http://api.vasttrafik.se/',
    version: '*'
  });
  var url = '/bin/rest.exe/v1/trip?authKey=c1b0e0ba-c41e-4f38-b166-ac55703f95fe&format=json&needJourneyDetail=0&originId=' + item.origin.id + '&destId=' + item.destination.id;
  if (date && time)
    url += '&date=' + date + '&time=' + time;
  client.get(url, function (err, req, res2, obj) {
    if (err)
      return callback(err, null);
    if (obj.TripList.error)
      return callback(new Error(obj.TripList.error + ': ' + obj.TripList.errorText), null);
    fixArrays(obj);

    var trips = [];
    for (var i = 0; i < obj.TripList.Trip.length; i++) {
      var trip = obj.TripList.Trip[i];

      // ignore trips that begins with a walk
      if (trip.Leg[0].type == 'WALK')
        continue;

      var legs = [];
      for (var j = 0; j < trip.Leg.length; j++) {
        var leg = trip.Leg[j];
        // ignore walk legs
        if (leg.type == 'WALK')
          continue;
        var departure;
        if (leg.Origin.rtDate && leg.Origin.rtTime)
          departure = new Date(leg.Origin.rtDate + 'T' + leg.Origin.rtTime + 'Z');
        else
          departure = new Date(leg.Origin.date + 'T' + leg.Origin.time + 'Z');
        var arrival;
        if (leg.Destination.rtDate && leg.Destination.rtTime)
          arrival = new Date(leg.Destination.rtDate + 'T' + leg.Destination.rtTime + 'Z');
        else
          arrival = new Date(leg.Destination.date + 'T' + leg.Destination.time + 'Z');
        var origin = leg.Origin.name.replace(', Göteborg', '').replace(', Partille', '');
        var destination = leg.Destination.name.replace(', Göteborg', '').replace(', Partille', '');
        legs.push({
          name: leg.name,
          direction: leg.direction,
          origin: origin + (leg.Origin.track ? ' ' + leg.Origin.track : ''),
          departure: departure,
          destination: destination + (leg.Destination.track ? ' ' + leg.Destination.track : ''),
          arrival: arrival
        });
      }
      var firstLeg = legs[0];
      var lastLeg = legs[legs.length - 1];
      var duration = new Date(lastLeg.arrival - firstLeg.departure);
      trips.push({
        origin: firstLeg.origin,
        destination: lastLeg.destination,
        departure: firstLeg.departure,
        arrival: lastLeg.arrival,
        duration: duration,
        legs: legs
      });
    }

    callback(null, {
      origin: item.origin.name,
      destination: item.destination.name,
      departure: trips.length > 0 ? trips[0].departure : null,
      duration: trips.length > 0 ? trips[0].duration : null,
      trips: trips
    });
  });
};

var toTime = function (date) {
  var h = date.getUTCHours();
  var m = date.getUTCMinutes();
  return (h < 10 ? '0' + h : h) + ':' + (m < 10 ? '0' + m : m);
};

var toDuration = function (date) {
  var h = date.getUTCHours();
  var m = date.getUTCMinutes();
  return h + 'h' + (m < 10 ? '0' + m : m) + 'm';
};

var toText = function (obj) {
  var text = '';
  for (var i = 0; i < obj.length; i++) {
    var o = obj[i];
    if (o.trips.length == 0) // probably only walk legs
      continue;
    text += o.origin + ' ' + toTime(o.departure) + '\n' +
      o.destination + ' ' + toDuration(o.duration) + '\n\n';
    for (var j = 0; j < o.trips.length; j++) {
      var trip = o.trips[j];
      text += '  ' + trip.origin + ' ' + toTime(trip.departure) + '\n' +
        '  ' + trip.destination + ' ' + toDuration(trip.duration) + '\n\n';
      for (var k = 0; k < trip.legs.length; k++) {
        var leg = trip.legs[k];
        text += '    ' + leg.name + '\n' +
          '    ' + leg.direction + '\n' +
          '    ' + leg.origin + ' ' + toTime(leg.departure) + '\n' +
          '    ' + leg.destination + ' ' + toTime(leg.arrival) + '\n\n';
      }
    }
  }
  return text;
};

var trip = function (req, res, next) {
  var searches = [{
    origin: {
      id: 9021014002130000,
      name: 'Domkyrkan'
    },
    destination: {
      id: 9021014013314000,
      name: 'Stubbvägen'
    }
  }, {
    origin: {
      id: 9021014001950000,
      name: 'Centralstationen'
    },
    destination: {
      id: 9021014013314000,
      name: 'Stubbvägen'
    }
  }, {
    origin: {
      id: 9021014004945000,
      name: 'Nordstan'
    },
    destination: {
      id: 9021014013314000,
      name: 'Stubbvägen'
    }
  }, {
    origin: {
      id: 9021014003127000,
      name: 'Heden'
    },
    destination: {
      id: 9021014013330000,
      name: 'Furulund'
    }
  }, {
    origin: {
      id: 9021014006480000,
      name: 'Svingeln'
    },
    destination: {
      id: 9021014013314000,
      name: 'Stubbvägen'
    }
  }, {
    origin: {
      id: 9021014013314000,
      name: 'Stubbvägen'
    },
    destination: {
      id: 9021014006480000,
      name: 'Svingeln'
    }
  }, {
    origin: {
      id: 9021014013330000,
      name: 'Furulund'
    },
    destination: {
      id: 9021014006480000,
      name: 'Svingeln'
    }
  }];

  async.map(searches, function (item, callback) {
    searchTrip(req.params.date, req.params.time, item, callback);
  }, function (err, results) {
    next.ifError(err);
    res.charSet('utf-8');
    if (req.params.format == 'json') {
      res.send(results);
    } else {
      res.setHeader('Content-Type', 'text/plain');
      res.send(toText(results));
    }
    next();
  });
};

server.get('/trip', trip);
server.post('/trip', trip);

server.listen(process.env.PORT, function () {
  console.log('%s listening at %s', server.name, server.url);
});