import 'package:travel_guide/features/models/county_detail.dart';
import 'package:travel_guide/features/services/country_specific_data_service.dart';

class CountriesSpecificDataRepository {
  factory CountriesSpecificDataRepository() {
    singleton ??= CountriesSpecificDataRepository._internal();
    return singleton!;
  }

  CountriesSpecificDataRepository._internal();
  static CountriesSpecificDataRepository? singleton;
  Future<CountyDetail?> getCountrySpecificResponse(
      {String? countryName}) async {
    return await CountriesSpecificDataService()
        .getCountriesSpecificDataServiceResponse(countryName: countryName);
  }
}
