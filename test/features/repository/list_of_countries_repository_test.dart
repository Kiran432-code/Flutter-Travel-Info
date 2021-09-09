import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:travel_guide/features/models/countries_list.dart';
import 'package:travel_guide/features/repository/list_of_countries_repository.dart';
import 'package:travel_guide/features/services/list_of_countries_service.dart';

import '../models/mock/mock_json_countries_list.dart';

class MockCountriesListService extends Fake implements CountriesListService {
  @override
  Future<List<CountriesList>?> getCountriesListServiceResponse() async {
    return CountriesList().fromJson(mockCountriesList);
  }
}

class MockCountriesListServiceBadData extends Fake
    implements CountriesListService {
  @override
  Future<List<CountriesList>?> getCountriesListServiceResponse() async {
    return null;
  }
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  late CountriesListRepository repository;
  setUpAll(() {
    repository = CountriesListRepository();
  });

  test('get successful data from repository', () async {
    await dotenv.load(fileName: ".env");
    final List<CountriesList>? response = await repository
        .getCountriesResponseList(service: MockCountriesListService());
    expect(response?.first.url,
        'https://travelbriefing.org/Afghanistan?format=json');
    expect(response?.first.name, 'Afghanistan');
  });
}
