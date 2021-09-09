import 'package:travel_guide/features/models/countries_list.dart';
import 'package:travel_guide/features/services/list_of_countries_service.dart';

class CountriesListRepository {
  factory CountriesListRepository() {
    singleton ??= CountriesListRepository._internal();
    return singleton!;
  }

  CountriesListRepository._internal();
  static CountriesListRepository? singleton;
  Future<List<CountriesList>?> getCountriesResponseList(
      {CountriesListService? service}) async {
    service ??= CountriesListService();
    return await service.getCountriesListServiceResponse();
  }
}
