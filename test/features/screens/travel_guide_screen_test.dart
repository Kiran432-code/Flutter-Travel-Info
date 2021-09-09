import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:rxdart/rxdart.dart';
import 'package:travel_guide/features/bloc/list_of_countries_bloc.dart';
import 'package:travel_guide/features/models/countries_list.dart';
import 'package:travel_guide/features/models/county_detail.dart';
import 'package:travel_guide/features/screens/travel_guide_screen.dart';
import 'package:travel_guide/features/services/list_of_countries_service.dart';

import '../models/mock/mock_country_detail.dart';
import '../models/mock/mock_json_countries_list.dart';

class MockNavigatorObserver extends Mock implements NavigatorObserver {}

class MockCountriesListDataBloc extends Fake implements CountriesListDataBloc {
  final BehaviorSubject<List<CountriesList>?> countriesListDataFetcher =
      BehaviorSubject<List<CountriesList>?>.seeded(null);

  ValueStream<List<CountriesList>?> get getCountriesListDataStream$ =>
      countriesListDataFetcher.stream;

  final BehaviorSubject<CountyDetail?> countrySpecificDataFetcher =
      BehaviorSubject<CountyDetail?>.seeded(null);

  ValueStream<CountyDetail?> get getCountrySpecificDataStream$ =>
      countrySpecificDataFetcher.stream;
  @override
  Future<void> getCountriesListData({CountriesListService? service}) async {
    countriesListDataFetcher.add(CountriesList().fromJson(mockCountriesList));
  }

  Future<void> getSpecificCountryData({String? countryName}) async {
    countrySpecificDataFetcher.add(CountyDetail.fromJson(mockCountryDetail));
  }

  void dispose() {
    countriesListDataFetcher.close();
    countrySpecificDataFetcher.close();
  }
}

void main() {
  MockNavigatorObserver mockNavigatorObserver = MockNavigatorObserver();

  Future<void> pumpScreen(WidgetTester tester) async {
    TravelGuideScreenState.countriesListDataBloc = MockCountriesListDataBloc();
    await tester.pumpWidget(MaterialApp(
      home: Material(child: TravelGuideScreen()),
      navigatorObservers: [mockNavigatorObserver],
    ));
    await tester.pumpAndSettle();
  }

  testWidgets('Test that TravelGuideScreen Screen working',
      (WidgetTester tester) async {
    await pumpScreen(tester);
    await tester.pumpAndSettle();
    expect(find.text('Travel Guide Links'), findsOneWidget);
    expect(find.text('Afghanistan'), findsOneWidget);
  });

  testWidgets('tap on the specific list tile', (WidgetTester tester) async {
    await tester.pumpAndSettle();
    await pumpScreen(tester);
    expect(find.text('Afghanistan'), findsOneWidget);
    await tester.tap(find.text('Afghanistan'));
    await tester.pumpAndSettle();
    verify(mockNavigatorObserver.didPush(any as dynamic, any));
    expect(find.text('Aland Islands'), findsOneWidget);
  });
}
