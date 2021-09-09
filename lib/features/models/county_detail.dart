class CountyDetail {
  CountyDetail({
    this.names,
    this.maps,
    this.timezone,
    this.telephone,
    this.currency,
    this.weather,
  });

  Names? names;
  Maps? maps;
  Timezone? timezone;
  Telephone? telephone;
  Currency? currency;
  Map<String, Weather>? weather;

  factory CountyDetail.fromJson(Map<String, dynamic>? json) => CountyDetail(
        names: Names.fromJson(json?["names"]),
        maps: Maps.fromJson(json?["maps"]),
        timezone: Timezone.fromJson(json?["timezone"]),
        telephone: Telephone.fromJson(json?["telephone"]),
        currency: Currency.fromJson(json?["currency"]),
        weather: Map.from(json?["weather"])
            .map((k, v) => MapEntry<String, Weather>(k, Weather.fromJson(v))),
      );
}

class Ca {
  Ca({
    this.advise,
    this.url,
  });

  String? advise;
  String? url;

  factory Ca.fromJson(Map<String, dynamic> json) => Ca(
        advise: json["advise"],
        url: json["url"],
      );
}

//
class Currency {
  Currency({
    this.name,
    this.code,
    this.symbol,
    this.rate,
    this.compare,
  });

  String? name;
  String? code;
  String? symbol;
  String? rate;
  List<Compare>? compare;

  factory Currency.fromJson(Map<String, dynamic> json) => Currency(
        name: json["name"],
        code: json["code"],
        symbol: json["symbol"],
        rate: json["rate"],
        compare:
            List<Compare>.from(json["compare"].map((x) => Compare.fromJson(x))),
      );
}

//
class Compare {
  Compare({
    this.name,
    this.rate,
  });

  String? name;
  String? rate;

  factory Compare.fromJson(Map<String, dynamic> json) => Compare(
        name: json["name"],
        rate: json["rate"],
      );
}

class Maps {
  Maps({
    this.lat,
    this.long,
  });

  String? lat;
  String? long;

  factory Maps.fromJson(Map<String, dynamic> json) => Maps(
        lat: json["lat"],
        long: json["long"],
      );

  Map<String, dynamic> toJson() => {
        "lat": lat,
        "long": long,
      };
}

class Names {
  Names({
    this.name,
    this.full,
    this.continent,
  });

  String? name;
  String? full;
  String? continent;

  factory Names.fromJson(Map<String, dynamic> json) => Names(
        name: json["name"],
        full: json["full"],
        continent: json["continent"],
      );
}

class Neighbor {
  Neighbor({
    this.id,
    this.name,
  });

  String? id;
  String? name;

  factory Neighbor.fromJson(Map<String, dynamic> json) => Neighbor(
        id: json["id"],
        name: json["name"],
      );

  Map<String, dynamic> toJson() => {
        "id": id,
        "name": name,
      };
}

class Telephone {
  Telephone({
    this.callingCode,
    this.police,
    this.ambulance,
    this.fire,
  });

  String? callingCode;
  String? police;
  String? ambulance;
  String? fire;

  factory Telephone.fromJson(Map<String, dynamic> json) => Telephone(
        callingCode: json["calling_code"],
        police: json["police"],
        ambulance: json["ambulance"],
        fire: json["fire"],
      );
}

//
class Timezone {
  Timezone({
    this.name,
  });

  String? name;

  factory Timezone.fromJson(Map<String, dynamic> json) => Timezone(
        name: json["name"],
      );
}

class Weather {
  Weather({
    this.tMin,
    this.tMax,
  });

  String? tMin;
  String? tMax;

  factory Weather.fromJson(Map<String, dynamic> json) => Weather(
        tMin: json["tMin"],
        tMax: json["tMax"],
      );
}
