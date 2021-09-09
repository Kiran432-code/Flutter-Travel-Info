class CountriesList {
  CountriesList({
    this.name,
    this.url,
  });

  String? name;
  String? url;

  factory CountriesList.fromMap(Map<String, dynamic> json) => CountriesList(
        name: json["name"],
        url: json["url"],
      );

  List<CountriesList> fromJson(List<dynamic> json) {
    return List<CountriesList>.from(
        json.map<CountriesList>((dynamic x) => CountriesList.fromMap(x)));
  }
}
