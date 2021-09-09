import 'package:flutter_test/flutter_test.dart';
import 'package:travel_guide/features/models/countries_list.dart';

import 'mock/mock_json_countries_list.dart';

void main() {
  test('Test json valid model data', () async {
    final List<CountriesList?> countriesList =
        CountriesList().fromJson(mockCountriesList);
    expect(countriesList[0]!.url,
        'https://travelbriefing.org/Afghanistan?format=json');
    expect(countriesList[0]!.name, 'Afghanistan');
  });

  test('Test empty model data', () async {
    final List<CountriesList?> countriesList = CountriesList().fromJson([]);
    expect(countriesList, []);
  });
}
