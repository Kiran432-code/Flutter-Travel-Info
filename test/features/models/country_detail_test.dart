import 'package:flutter_test/flutter_test.dart';
import 'package:travel_guide/features/models/county_detail.dart';

import 'mock/mock_country_detail.dart';

void main() {
  test('Test json valid model data for countryDetail', () async {
    final CountyDetail? countryDetails =
        CountyDetail.fromJson(mockCountryDetail);
    expect(countryDetails!.currency!.name, 'Euro');
    expect(countryDetails.names!.name, 'Aland Islands');
    expect(countryDetails.maps!.lat, '0.199542');
  });
}
