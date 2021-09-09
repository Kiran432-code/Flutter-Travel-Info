import 'dart:convert';

import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart';
import 'package:http/testing.dart';
import 'package:travel_guide/features/models/countries_list.dart';
import 'package:travel_guide/features/services/custom_http_exception.dart';
import 'package:travel_guide/features/services/list_of_countries_service.dart';

import '../models/mock/mock_json_countries_list.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('get successful response form countries api', () async {
    await dotenv.load(fileName: ".env");
    CountriesListService.client = MockClient((Request request) async {
      return Response(json.encode(mockCountriesList), 200);
    });
    List<CountriesList>? response =
        await CountriesListService().getCountriesListServiceResponse();
    expect(response!.first.name, 'Afghanistan');
    expect(response.first.url,
        'https://travelbriefing.org/Afghanistan?format=json');
    expect(response.last.name, 'Taiwan');
    expect(response.last.url, 'https://travelbriefing.org/Taiwan?format=json');
  });

  test('throw Http500Exception for bad response from countries api', () async {
    await dotenv.load(fileName: ".env");
    CountriesListService.client = MockClient((Request request) async {
      throw InternalServerException(
          'Error occurred while Communication with Server with StatusCode',
          uri: Uri(path: ''));
    });

    expect(() => CountriesListService().getCountriesListServiceResponse(),
        throwsA(isInstanceOf<InternalServerException>()));
  });
}
